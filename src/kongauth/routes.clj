(ns kongauth.routes
  (:require
   [compojure.route :as route]
   [compojure.core :refer [routes GET POST ANY]]
   [ring.util.response :refer [response content-type charset]]))

(defn auth-routes [e]
  (routes
   (GET "/" [] "Hello world")))
