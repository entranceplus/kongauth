(ns kongauth.systems
  (:require [com.stuartsierra.component :as component]
            [environ.core :refer [env]]
            [kongauth.routes :refer [auth-routes]]
            [snow.db :refer [get-db-spec-from-env]]
            [snow.systems :as system]
            [maarschalk.konserve :as m]
            [ring.middleware.format :refer [wrap-restful-format]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            (system.components
             [jetty :refer [new-jetty]]
             [immutant-web :refer [new-immutant-web]]
             [endpoint :refer [new-endpoint]]
             [middleware :refer [new-middleware]]
             [repl-server :refer [new-repl-server]]
             [postgres :refer [new-postgres-database]]
             [kampbell :as kampbell]
             [konserve :as konserve]
             [handler :refer [new-handler]])))


(def rest-middleware
  (fn [handler]
    (wrap-restful-format handler
                         :formats [:json-kw]
                         :response-options {:json-kw {:pretty true}})))

(defn system-config [config]
  [:db (new-postgres-database (get-db-spec-from-env :config config))
   :user-db (konserve/new-konserve :type :filestore
                                   :path (config :db-path)
                                   :serializer (m/fressian-serializer))
   :user-store (component/using
                 (kampbell/new-kampbell :equality-specs #{:domain.utils/created-at}
                                        :entities #{"users"})
                 [:user-db])
   :auth (component/using
          (new-endpoint auth-routes)
          [:db])
   :middleware (new-middleware
                {:middleware  [rest-middleware
                               [wrap-defaults api-defaults]]})
   :handler (component/using
             (new-handler)
             [:auth :middleware])
   :web (component/using (new-immutant-web :port (system/get-port config))
                         [:handler])])

(defn dev-system []
  (system/gen-system system-config))

(defn prod-system
  "Assembles and returns components for a production deployment"
  []
  (merge (dev-system)
         (component/system-map
          :repl-server (new-repl-server (read-string (env :repl-port))))))
