(ns server.schema
  (:require [malli.util :as mu]
            [malli.core :as m]
            [malli.error :as me]))

(defn with-validation
  "schema->data — a map of schema => data
   valid — a function to call when all data is valid
   not-valid — a function to call when some data is not valid"
  [schema->data & {:keys [valid not-valid]}]
  (if (every? #(apply m/validate %) schema->data)
    (valid)
    (not-valid
      (->> schema->data
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