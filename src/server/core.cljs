(ns server.core
  (:require [reitit.core :as r]
            [lib.async :refer [js-await]]
            [server.db :as db]
            [server.cf :as cf]
            [server.schema :as schema]))

(def router
  (r/router
    ["/api"
     ["/todos" ::todos]
     ["/todos/:id" ::todo]]))

;; args:
;;  route: Reitit route data
;;  request: js/Request object https://developers.cloudflare.com/workers/runtime-apis/request/
;;  env: Environment object containig env vars and bindings to Cloudflare services https://developers.cloudflare.com/workers/configuration/environment-variables/
;;  ctx: The Context API provides methods to manage the lifecycle of your Worker https://developers.cloudflare.com/workers/runtime-apis/context/
(defmulti handle-route (fn [route request env ctx]
                         [(-> route :data :name) (keyword (.-method ^js request))]))

(defmethod handle-route [::todos :GET] [route request env ctx]
  (js-await [{:keys [success results]} (db/query+ {:select [:*]
                                                   :from   [:todo]})]
    (if success
      (cf/response-edn {:result results} {:status 200})
      (cf/response-error))))

(defmethod handle-route [::todos :POST] [route request env ctx]
  (js-await [{:keys [title description due_date status]} (cf/request->edn request)
             todo {:id (str (random-uuid))
                   :title title
                   :description description
                   :due_date due_date
                   :status status}]
    (schema/with-validation {schema/NewTodo todo}
      :valid
      (fn []
        (js-await [{:keys [success results]} (db/run+ {:insert-into [:todo] :values [todo]})]
          (if success
            (cf/response-edn {:result results} {:status 200})
            (cf/response-error))))
      :not-valid
      (fn [errors]
        (cf/response-error errors)))))

(defmethod handle-route [::todo :POST] [route request env ctx]
  (js-await [{:keys [id title description due_date status]} (cf/request->edn request)
             todo {:id id
                   :title title
                   :description description
                   :due_date due_date
                   :status status}]
    (schema/with-validation {schema/NewTodo todo}
      :valid
      (fn []
        (js-await [{:keys [success results]} (db/run+ {:update [:todo]
                                                       :set     (dissoc todo :id)
                                                       :where   [:= :id id]})]
          (if success
            (cf/response-edn {:result results} {:status 200})
            (cf/response-error))))
      :not-valid
      (fn [errors]
        (cf/response-error errors)))))

(defmethod handle-route [::todo :DELETE] [{:keys [path-params]} request env ctx]
  (let [{:keys [id]} path-params]
    (schema/with-validation {schema/TodoId id}
      :valid
      (fn []
        (js-await [{:keys [success results]} (db/run+ {:delete-from [:todo]
                                                       :where        [:= :id id]})]
          (if success
            (cf/response-edn {:result results} {:status 200})
            (cf/response-error))))
      :not-valid
      (fn [errors]
        (cf/response-error errors)))))

;; entry point
(def handler
  #js {:fetch (cf/with-handler router handle-route)})