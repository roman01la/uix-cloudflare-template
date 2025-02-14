(ns server.core
  (:require [reitit.core :as r]
            [shadow.cljs.modern :refer [js-await]]
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
  (js-await [{:keys [success results]} (db/query "SELECT * FROM todo")]
    (if success
      (cf/response-edn {:result results} {:status 200})
      (cf/response-error))))

(defmethod handle-route [::todos :POST] [route request env ctx]
  (js-await [{:keys [title description due_date status]} (cf/request->end request)]
    (js-await [{:keys [success results]} (db/run "INSERT INTO todo (id, title, description, due_date, status) VALUES (?, ?, ?, ?, ?)"
                                            [(str (random-uuid)) title description due_date status])]
      (if success
        (cf/response-edn {:result results} {:status 200})
        (cf/response-error)))))

(defmethod handle-route [::todo :POST] [route request env ctx]
  (js-await [{:keys [id title description due_date status]} (cf/request->end request)]
    (js-await [{:keys [success results]} (db/run "UPDATE todo SET title = ?, description = ?, due_date = ?, status = ? WHERE id = ?"
                                            [title description due_date status id])]
      (if success
        (cf/response-edn {:result results} {:status 200})
        (cf/response-error)))))

(defmethod handle-route [::todo :DELETE] [{:keys [path-params]} request env ctx]
  (let [{:keys [id]} path-params]
    (js-await [{:keys [success results]} (db/run "DELETE FROM todo WHERE id = ?" [id])]
      (if success
        (cf/response-edn {:result results} {:status 200})
        (cf/response-error)))))

;; entry point
(def handler
  #js {:fetch (cf/with-handler router handle-route)})