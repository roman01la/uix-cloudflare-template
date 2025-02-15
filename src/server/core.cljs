(ns server.core
  (:require [reitit.core :as r]
            [lib.async :refer [js-await]]
            [server.db :as db]
            [server.cf :as cf]))

(def router
  (r/router
    ["/api"
     ["/todos" ::todos]
     ["/todos/:id" ::todo]]))

(defmulti handle-route (fn [route request env ctx]
                         [(-> route :data :name) (keyword (.-method ^js request))]))

(defmethod handle-route [::todos :GET] [route request env ctx]
  (js-await [{:keys [success results]} (db/query {:select [:*]
                                                  :from   [:todo]})]
    (if success
      (cf/response-edn {:result results} {:status 200})
      (cf/response-error))))

(defmethod handle-route [::todos :POST] [route request env ctx]
  (js-await [{:keys [title description due_date status]} (cf/request->end request)
             {:keys [success results]} (db/run {:insert-into [:todo]
                                                :columns [:id :title :description :due_date :status]
                                                :values [[(str (random-uuid)) title description due_date status]]})]
    (if success
      (cf/response-edn {:result results} {:status 200})
      (cf/response-error))))

(defmethod handle-route [::todo :POST] [route request env ctx]
  (js-await [{:keys [id title description due_date status]} (cf/request->end request)
             {:keys [success results]} (db/run {:update [:todo]
                                                :set {:title title
                                                      :description description
                                                      :due_date due_date
                                                      :status status}
                                                :where [:= :id id]})]
    (if success
      (cf/response-edn {:result results} {:status 200})
      (cf/response-error))))

(defmethod handle-route [::todo :DELETE] [{:keys [path-params]} request env ctx]
  (let [{:keys [id]} path-params]
    (js-await [{:keys [success results]} (db/run {:delete-from [:todo]
                                                  :where [:= :id id]})]
      (if success
        (cf/response-edn {:result results} {:status 200})
        (cf/response-error)))))

;; entry point
(def handler
  #js {:fetch (cf/with-handler router handle-route)})