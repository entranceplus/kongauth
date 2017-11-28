(ns kongauth.util
  (:require [ring.util.http-response :as response]))

(defn send-response [response]
  (-> response
      (response/header "Content-Type" "application/json; charset=utf-8")))

(defn send-err-response [response]
  (-> response
      (response/header "Content-Type" "application/json; charset=utf-8")))

(defn ok-response [response]
  (-> (response/ok response)
      send-response))
