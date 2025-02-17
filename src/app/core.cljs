(ns app.core
  (:require [uix.core :as uix :refer [defui $]]
            [uix.dom]
            [app.api :as api]
            [app.hooks :as hooks]))

(defui text-field [props]
  ($ :input.input.input-bordered.w-full.max-w-xs.text-zinc-900 props))

(defui todo-readonly [{:keys [item on-click]}]
  (let [{:keys [id title description due_date status]} item]
    ($ :.card.bg-primary.text-primary-content.w-96 {:on-click on-click}
       ($ :.card-body
         ($ :h3.card-title title)
         ($ :div description)
         ($ :.flex.items-center.gap-4
          ($ :div (str "Due date: " (.toLocaleString (js/Date. due_date))))
          ($ :.status {:style {:background-color (get {"pending" "red"
                                                       "completed" "green"}
                                                      status)}}))))))

(defui todo-edit [{:keys [item on-save on-cancel on-delete]}]
  (let [[{:keys [id title description due_date status] :as item} set-state] (uix/use-state item)]
    ($ :.card.bg-primary.text-primary-content.w-96
       ($ :.card-body
         ($ text-field {:value title
                        :placeholder "Title"
                        :on-change #(set-state assoc :title (.. % -target -value))})
         ($ text-field {:value description
                        :placeholder "Description"
                        :on-change #(set-state assoc :description (.. % -target -value))})
         ($ text-field {:type :date
                        :value due_date
                        :on-change #(set-state assoc :due_date (.. % -target -value))})
         ($ :.flex.justify-between.gap-x-2
           ($ :button.btn {:on-click #(on-save item)}
              "Save")
           ($ :button.btn {:on-click on-cancel}
              "Cancel")
           ($ :.flex-1)
           ($ :button.btn.btn-circle {:on-click on-delete}
              ($ :svg
                 {:xmlns "http://www.w3.org/2000/svg"
                  :className "h-6 w-6"
                  :fill "none"
                  :viewBox "0 0 24 24"
                  :stroke "currentColor"}
                 ($ :path
                    {:strokeLinecap "round"
                     :strokeLinejoin "round"
                     :strokeWidth "2"
                     :d "M6 18L18 6M6 6l12 12"}))))))))

(defui todo-create [{:keys [on-save]}]
  (let [[{:keys [title description due_date] :as item} set-state]
        (uix/use-state {:title ""
                        :description ""
                        :due_date ""
                        :status "pending"})]
    ($ :.card.bg-primary.text-primary-content.w-96
       ($ :.card-body
         ($ text-field
            {:value title
             :placeholder "Title"
             :on-change #(set-state assoc :title (.. % -target -value))})
         ($ text-field
            {:value description
             :placeholder "Description"
             :on-change #(set-state assoc :description (.. % -target -value))})
         ($ :.flex.gap-x-2
            ($ text-field
               {:type :date
                :value due_date
                :on-change #(set-state assoc :due_date (.. % -target -value))})
            ($ :button.btn {:on-click #(on-save item)}
               "Create"))))))

(defui todo [{:keys [item]}]
  (let [{:keys [id] :as item} item
        [{:keys [editing?]} set-state] (uix/use-state {:editing? false})
        update-todo (hooks/use-mutation api/update-todo+ :invalidates [:todos])
        delete-todo (hooks/use-mutation api/delete-todo+ :invalidates [:todos])]
    (if editing?
      ($ todo-edit {:item item
                    :on-save #(do (update-todo %)
                                  (set-state assoc :editing? false))
                    :on-cancel #(set-state assoc :editing? false)
                    :on-delete #(delete-todo id)})
      ($ todo-readonly {:item item
                        :on-click #(set-state assoc :editing? true)}))))

(defui user-presence [{:keys [users]}]
  ($ :.relative.h-3
    (map-indexed
      (fn [idx uid]
        ($ :.w-3.h-3.rounded-full.bg-green-500.absolute.border-2.border-white
           {:key uid
            :style {:left (* idx 8)}}))
      users)))

(defonce uid (str (js/Date.now)))

(defui app []
  (let [presence (hooks/use-presence uid)
        {:keys [data isLoading isError isSuccess]} (hooks/use-query [:todos] api/get-todos+)
        create-todo (hooks/use-mutation api/create-todo+ :invalidates [:todos])]
    ($ :.flex.items-center.justify-center.h-screen
      {:data-theme :light}
      (cond
        isLoading ($ :p "Loading...")
        isError ($ :p "Couldn't load data")
        isSuccess
        ($ :.flex.flex-col.gap-y-2
          ($ user-presence {:users presence})
          (for [item data]
            ($ todo {:key (:id item)
                     :item item}))
          ($ todo-create
             {:on-save create-todo}))))))

(defui app-root []
  ($ hooks/query-client-provider
     ($ app)))

(defonce root
  (uix.dom/create-root (js/document.getElementById "root")))

(defn ^:export init []
  (uix.dom/render-root ($ uix/strict-mode ($ app-root))
                       root))
