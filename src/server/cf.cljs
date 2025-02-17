(ns server.cf
  (:refer-clojure :exclude [js->clj])
  (:require-macros [server.cf])
  (:require [clojure.edn :as edn]
            [reitit.core :as r]
            [lib.async :refer [js-await]]
            [goog.object]))

;; each incoming request to a worker binds the following vars
;; for ease of access throughout the codebase
(def DB (atom nil))
(def ENV (atom nil))
(def CTX (atom nil))

(defn response [body init]
  ;; https://developers.cloudflare.com/workers/runtime-apis/response/
  (js/Response. body (clj->js init)))

(defn response-edn
  "Like `response`, but takes Clojure data and serializes it to EDN string"
  [body init]
  (response (pr-str body)
            (assoc-in init [:headers "Content-Type"] "text/edn")))

(defn response-error
  ([]
   (response-edn {:error "Something went wrong"} {:status 200}))
  ([error]
   (response-edn {:error error} {:status 200})))

(defn request->edn
  "Reads the request body as EDN"
  [^js request]
  (js-await [text (.text request)]
    (edn/read-string text)))

(defn- with-params [^js/URL url route]
  (assoc route :query-params
    (->> (.entries (.-searchParams url))
         (reduce (fn [ret [k v]]
                   (assoc ret (keyword k) v))
                 {}))))

(defn with-handler
  "Given a Reitit router and a handler function, returns an entry point function for Cloudflare Worker"
  [router handler]
  (fn [request ^js env ctx]
    (let [url (js/URL. (.-url request))
          route (r/match-by-path router (.-pathname url))]
      (reset! ENV env)
      (reset! CTX ctx)
      (reset! DB (.-DB env))
      (js/Promise. (fn [resolve reject]
                     (resolve (handler (with-params url route) request env ctx)))))))

(defn js->clj
  "Recursively transforms JavaScript arrays into ClojureScript
  vectors, and JavaScript objects into ClojureScript maps.  With
  option ':keywordize-keys true' will convert object fields from
  strings to keywords."
  ([x] (js->clj x :keywordize-keys false))
  ([x & opts]
   (let [{:keys [keywordize-keys]} opts
         keyfn (if keywordize-keys keyword str)
         f (fn thisfn [x]
             (cond
               (satisfies? IEncodeClojure x)
               (-js->clj x (apply array-map opts))

               (seq? x)
               (doall (map thisfn x))

               (map-entry? x)
               (MapEntry. (thisfn (key x)) (thisfn (val x)) nil)

               (coll? x)
               (into (empty x) (map thisfn) x)

               (array? x)
               (persistent!
                (reduce #(conj! %1 (thisfn %2))
                        (transient []) x))

               (identical? (type x) js/Object)
               (persistent!
                (reduce (fn [r k] (assoc! r (keyfn k) (thisfn (goog.object/get x k))))
                        (transient {}) (js-keys x)))

               (instance? js/Map x)
               (persistent!
                 (reduce (fn [r [k v]] (assoc! r (keyfn k) (thisfn v)))
                         (transient {}) (.entries x)))

               (instance? js/Set x)
               (persistent!
                 (reduce (fn [r v] (conj! r (thisfn v)))
                         (transient #{}) (.values x)))

               :else x))]
     (f x))))