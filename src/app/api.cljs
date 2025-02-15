(ns app.api
  (:require [clojure.edn :as edn]))

(defn- ^js/Promise handle-response+ [^js/Promise resp]
  (-> resp
      (.then #(.text %))
      (.then #(let [{:keys [error result]} (edn/read-string %)]
                (if error
                  (throw error)
                  result)))))

(defn api-request+
  ([path]
   (handle-response+ (js/fetch (str "/api" path))))
  ([path {:keys [method body]}]
   (handle-response+
     (js/fetch (str "/api" path)
               #js {:method method
                    :headers #js {"Content-Type" "text/edn"}
                    :body (pr-str body)}))))

(defn create-todo+ [item]
  (api-request+ "/todos" {:method "POST"
                          :body    item}))

(defn update-todo+ [{:keys [id] :as item}]
  (api-request+ (str "/todos/" id) {:method "POST"
                                    :body    item}))

(defn delete-todo+ [id]
  (api-request+ (str "/todos/" id) {:method "DELETE"}))

(defn get-todos+ []
  (api-request+ "/todos"))
