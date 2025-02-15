(ns server.db
  (:require [lib.async :refer [js-await]]
            [server.cf :as cf]
            [honey.sql :as sql]))

;; D1 docs https://developers.cloudflare.com/d1/

(defn ^js/Promise query+ [query]
  (let [[query & args] (sql/format query)]
    (js-await [result (.run (.prepare ^js @cf/DB query))]
      (js->clj result :keywordize-keys true))))

(defn ^js/Promise run+ [query]
  (let [[query & args] (sql/format query)
        stmt (.prepare ^js @cf/DB query)]
    (js-await [result (.run (.apply (.-bind stmt) stmt (into-array args)))]
      (js->clj result :keywordize-keys true))))
