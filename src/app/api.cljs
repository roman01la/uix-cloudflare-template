(ns app.api
  (:require [clojure.edn :as edn]))

(defn api-request
  ([path]
   (-> (js/fetch (str "/api" path))
       (.then #(.text %))
       (.then #(let [{:keys [error result]} (edn/read-string %)]
                 (if error
                   (throw error)
                   result)))))
  ([path {:keys [method body]}]
   (-> (js/fetch (str "/api" path)
                 #js {:method method
                      :headers #js {"Content-Type" "text/edn"}
                      :body (pr-str body)})
       (.then #(.text %))
       (.then #(let [{:keys [error result]} (edn/read-string %)]
                 (if error
                   (throw error)
                   result))))))

(defn create-todo [item]
  (api-request "/todos" {:method "POST"
                         :body item}))

(defn update-todo [{:keys [id] :as item}]
  (api-request (str "/todos/" id) {:method "POST"
                                   :body item}))

(defn delete-todo [id]
  (api-request (str "/todos/" id) {:method "DELETE"}))

(defn get-todos []
  (api-request "/todos"))
