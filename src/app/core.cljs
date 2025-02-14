(ns app.core
  (:require [uix.core :as uix :refer [defui $]]
            [uix.dom]
            [app.api :as api]
            [app.hooks :as hooks]))

(defui todo-readonly [{:keys [item on-click]}]
  (let [{:keys [id title description due_date status]} item]
    ($ :.todo-item {:on-click on-click}
       ($ :h3.title title)
       ($ :.desc description)
       ($ :.row
        ($ :div (str "Due date: " (.toLocaleString (js/Date. due_date))))
        ($ :.status {:style {:background-color (get {"pending" "red"
                                                     "completed" "green"}
                                                    status)}})))))

(defui todo-edit [{:keys [item on-save on-cancel on-delete]}]
  (let [[{:keys [id title description due_date status] :as item} set-state] (uix/use-state item)]
    ($ :.todo-item
       ($ :input.title {:value title
                        :on-change #(set-state assoc :title (.. % -target -value))})
       ($ :input.desc {:value description
                       :on-change #(set-state assoc :description (.. % -target -value))})
       ($ :.row
          ($ :input {:type :date
                     :value due_date
                     :on-change #(set-state assoc :due_date (.. % -target -value))})
          ($ :button {:on-click #(on-save item)}
             "Save")
          ($ :button {:on-click on-cancel}
             "Cancel")
          ($ :button {:on-click on-delete}
             "×")))))

(defui todo-create [{:keys [on-save]}]
  (let [[{:keys [title description due_date] :as item} set-state]
        (uix/use-state {:title ""
                        :description ""
                        :due_date ""
                        :status "pending"})]
    ($ :.todo-item
       ($ :input.title {:value title
                        :on-change #(set-state assoc :title (.. % -target -value))})
       ($ :input.desc {:value description
                       :on-change #(set-state assoc :description (.. % -target -value))})
       ($ :.row
          ($ :input {:type :date
                     :value due_date
                     :on-change #(set-state assoc :due_date (.. % -target -value))})
          ($ :button {:on-click #(on-save item)}
             "Create")))))

(defui todo [{:keys [item refetch]}]
  (let [{:keys [id title description due_date status] :as item} item
        [{:keys [editing?]} set-state] (uix/use-state {:editing? false})]
    (if editing?
      ($ todo-edit {:item item
                    :on-save #(do (-> (api/update-todo %)
                                      (.then refetch))
                                  (set-state assoc :editing? false))
                    :on-cancel #(set-state assoc :editing? false)
                    :on-delete #(-> (api/delete-todo id)
                                    (.then refetch))})
      ($ todo-readonly {:item item
                        :on-click #(set-state assoc :editing? true)}))))

(defui app []
  (let [[todos refetch] (hooks/use-fetch api/get-todos)]
    ($ :.app
      (for [item todos]
        ($ todo {:key (:id item)
                 :refetch refetch
                 :item item}))
      ($ todo-create
         {:on-save #(-> (api/create-todo %)
                        (.then refetch))}))))

(defonce root
  (uix.dom/create-root (js/document.getElementById "root")))

(defn ^:export init []
  (uix.dom/render-root ($ app) root))
