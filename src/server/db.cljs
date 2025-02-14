(ns server.db
  (:require [shadow.cljs.modern :refer [js-await]]
            [server.cf :as cf]))

;; D1 docs https://developers.cloudflare.com/d1/

(defn query [query]
  (js-await [result (.run (.prepare ^js @cf/DB query))]
    (js->clj result :keywordize-keys true)))

(defn run [query values]
  (let [stmt (.prepare ^js @cf/DB query)]
    (js-await [result (.run (.apply (.-bind stmt) stmt (into-array values)))]
      (js->clj result :keywordize-keys true))))
