(ns server.schema
  (:require [malli.util :as mu]
            [malli.core :as m]
            [malli.error :as me]))

(defn with-validation [schema->data & {:keys [valid error]}]
  (if (every? #(apply m/validate %) schema->data)
    (valid)
    (error (->> schema->data
                (remove #(apply m/validate %))
                (map #(apply m/explain %))
                (map me/humanize)))))

(def TodoId :string)

(def NewTodo
  [:map
   [:id #'TodoId]
   [:title :string]
   [:description :string]
   [:due_date :string]
   [:status :string]])

(def Todo
  (mu/merge
    NewTodo
    [:map
     [:created_at :string]
     [:updated_at :string]]))