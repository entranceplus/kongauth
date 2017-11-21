(ns kongauth.systems
  (:require [com.stuartsierra.component :as component]
            [environ.core :refer [env]]
            [kongauth.routes :refer [auth-routes]]
            (system.components
             [jetty :refer [new-jetty]]
             [endpoint :refer [new-endpoint]]
             [middleware :refer [new-middleware]]
             [handler :refer [new-handler]])))

(defn dev-system []
  (component/system-map
   :auth (new-endpoint auth-routes)
   :handler (component/using
             (new-handler)
             [:auth])
   :web (component/using (new-jetty :port (Integer. (env :http-port)))
                         [:handler])))
