(ns app.hooks
  (:require [uix.core :as uix :refer [$ defui defhook]]
            ["@tanstack/react-query" :as rq]
            [cljs-bean.core :as bean]))

(defonce query-client (rq/QueryClient.))

(defui query-client-provider [{:keys [children]}]
  ($ rq/QueryClientProvider {:client query-client}
     children))

;; https://tanstack.com/query/v4/docs/framework/react/quick-start
(defhook use-query
  "query-key - https://tanstack.com/query/v4/docs/framework/react/guides/query-keys
  query-fn - https://tanstack.com/query/v4/docs/framework/react/guides/query-functions"
  [query-key query-fn]
  (bean/->clj (rq/useQuery #js {:queryKey (clj->js query-key) :queryFn query-fn})))

;; https://tanstack.com/query/v4/docs/framework/react/guides/mutations
;; https://tanstack.com/query/v4/docs/framework/react/guides/invalidations-from-mutations
(defhook use-mutation
  "mutation-fn - promise returning fetch function
  on-success - function to run after mutation is successful
  invalidates - query keys to invalidate after mutation is successful"
  [mutation-fn & {:keys [on-success invalidates]}]
  (let [mutation (rq/useMutation
                   #js {:mutationFn mutation-fn
                        :onSuccess (fn []
                                     (when on-success (on-success))
                                     (doseq [query-key invalidates]
                                       (.invalidateQueries query-client #js {:queryKey (clj->js query-key)})))})]
    (specify! (bean/->clj mutation)
      Fn
      IFn
      (-invoke
        ([this data]
         (.mutate mutation data))))))