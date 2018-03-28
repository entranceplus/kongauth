(ns kongauth.core-test
  (:require [clojure.test :refer :all]
            [snow.client :as c]
            [environ.core :refer [env]]
            [kongauth.core :refer :all]))

(def base-url (str "http://localhost:" (env :http-port)))

(defn api-url [& endpoint]
  (str base-url "/" (first endpoint)))

(def user {:username "shakdwipeea"
           :password "hello"})

(defn get-user-token [user]
  (-> (api-url "auth")
      (c/post :body user)
      (get-in [:body :token])))

(defn user-present? [user]
  (-> (str "users/" (:username user))
      api-url
      c/get
      :present))

(defn test-fixture [f]
  (f))

(deftest auth-tests
  (testing "auth"
    (is (some? (c/get (api-url))))
    (is (some? (get-user-token user)))
    (is (false? (user-present? user)))))
