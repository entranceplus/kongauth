(ns kongauth.systems
  (:require [com.stuartsierra.component :as component]
            [environ.core :refer [env]]
            [kongauth.routes :refer [auth-routes]]
            [kongauth.db.util :refer [get-db-spec-from-env]]
            [ring.middleware.format :refer [wrap-restful-format]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            (system.components
             [jetty :refer [new-jetty]]
             [endpoint :refer [new-endpoint]]
             [middleware :refer [new-middleware]]
             [postgres :refer [new-postgres-database]]
             [handler :refer [new-handler]])))

(def rest-middleware
  (fn [handler]
    (wrap-restful-format handler
                         :formats [:json-kw]
                         :response-options {:json-kw {:pretty true}})))

(defn dev-system []
  (component/system-map
   :db (new-postgres-database (get-db-spec-from-env))
   :auth (component/using
          (new-endpoint auth-routes)
          [:db])
   :middleware (new-middleware
                {:middleware  [rest-middleware
                               [wrap-defaults api-defaults]]})
   :handler (component/using
             (new-handler)
             [:auth :middleware])
   :web (component/using (new-jetty :port (Integer. (env :http-port)))
                         [:handler])))
