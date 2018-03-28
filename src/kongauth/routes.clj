(ns kongauth.routes
  (:require [buddy.auth.backends :as backends]
            [buddy.sign.jwt :as jwt]
            [clj-http.client :as client]
            [compojure.core :refer [GET POST routes]]
            [crypto.password.pbkdf2 :as password]
            [kongauth.db.core :as authdb]
            [kongauth.db.util :as dbutil]
            [kongauth.util :as util]
            [cheshire.core :as json]
            [ring.util.http-response :as response]
            [slingshot.slingshot :refer [try+]])
  (:import [kongauth.db.core Postgres]))

(def secret "a-very-secret-string")

(def backend (backends/jws {:secret secret}))

(defn jwt-sign [claims]
  (println "Signing claims " claims)
  (jwt/sign claims secret {:alg :hs512}))

;; moving forward we can expose these (following comments)  params that we
;; are reading from
;; config files to be driven by sth else.. that process will be
;; the steps required to add the auth to a new project. I think config
;; files should work

;; read this from a config file
(def oauth-config {:client_id "JOERouFGerPXCvAtCOWvdg1DIhzRhUum"
                   :client_secret "T94S0O6RII3dmfpXA5MYRjOeBOIrsWOY"})

;; todo
;; pick the url from config files
;; in the request for token, the client will then also
;; have to send their app name and we can select that based on
;; what we have from config
(defn get-token
  "get token from kong"
  [{:keys [id username password]}]
  (-> "https://links.entranceplus.in/oauth2/token"
      (client/post {:form-params (merge oauth-config
                                        {:authenticated_userid id
                                         :grant_type "password"
                                         :scope "username"
                                         :provision_key "function"
                                         :username username
                                         :password password})
                    :as :json
                    :content-type :json})
      :body))

(defn get-refresh-token
  "get refresh token from kong"
  [refresh-token]
  (-> "https://links.entranceplus.in/oauth2/token"
      (client/post {:form-params (merge oauth-config
                                        {:grant_type "refresh_token"
                                         :refresh_token refresh-token})
                    :as :json
                    :content-type :json})
      :body))

;; (def db (:db system.repl/system))

(defn ensure-user
  "if username and password combo is present then get user,
  if not then create user"
  [db {:keys [username password] :as user}]
  (if-let  [{:keys [pass id]} (->> {:username username}
                                   (authdb/get-users (Postgres. db))
                                   seq
                                   first)]
    (when (password/check password pass)
        (merge user {:id id}))
    (let [id  (dbutil/uuid)
          user {:id id
                :username username
                :pass (password/encrypt password)}]
      (authdb/create-user (Postgres. db) user)
      user)))

(defn handle-auth
  "issue token for this user"
  [db user]
  (some->> user
           (ensure-user db)
           jwt-sign))

; (def user {:id "abc"
;            :username "shakdwipeea"
;            :password "hello"})
;
; (handle-auth (:db system.repl/system) user)
;


(defn user? [db username]
  (empty? (authdb/get-users (Postgres. db) {:username username})))

(defn auth-routes [{db :db}]
  (routes
   (GET "/" [] (util/ok-response {:msg "Move ahead, you shall!!"}))
   (POST "/auth" {user-info :params}
        (if-let [token (handle-auth db user-info)]
          (util/ok-response {:token token})
          (util/send-response (response/bad-request
                               {:reason "Incorrect credentials"}))))
   (POST "/refresh" {{refresh-token :refresh-token} :params}
         (util/ok-response
          (try+ (get-refresh-token refresh-token)
                (catch [:status 400] {:keys [body]}
                  (json/parse-string body true)))))
   (GET "/users/:username" [username]
        (util/ok-response {:present (user? db username)}))))
