(ns server.cf
  (:require [clojure.edn :as edn]
            [reitit.core :as r]
            [lib.async :refer [js-await]]))

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
                     (resolve (handler route request env ctx)))))))