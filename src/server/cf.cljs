(ns server.cf
  (:require [clojure.edn :as edn]
            [reitit.core :as r]
            [shadow.cljs.modern :refer [js-await]]))

(def DB (atom nil))
(def ENV (atom nil))
(def CTX (atom nil))

(defn response [body init]
  ;; https://developers.cloudflare.com/workers/runtime-apis/response/
  (js/Response. body (clj->js init)))

(defn response-edn [body init]
  (response (pr-str body)
            (assoc-in init [:headers "Content-Type"] "text/edn")))

(defn response-error []
  (response-edn {:error "Something went wrong"} {:status 200}))

(defn request->end [^js request]
  (js-await [text (.text request)]
    (edn/read-string text)))

(defn with-handler [router handler]
  (fn [request ^js env ctx]
    (let [url (js/URL. (.-url request))
          route (r/match-by-path router (.-pathname url))]
      (reset! ENV env)
      (reset! CTX ctx)
      (reset! DB (.-DB env))
      (js/Promise.resolve (handler route request env ctx)))))