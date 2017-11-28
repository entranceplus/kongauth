(ns kongauth.db.core
  (:require [kongauth.db.util :as dbutil]
            [honeysql.helpers :as helpers :refer :all]))

(defn create-user [db user]
  (dbutil/execute! db (-> (insert-into :users)
                          (values [user]))))

(defn get-users [db {:keys [username]}]
  (dbutil/query db {:select [:id :pass]
                    :from [:users]
                    :where [:= :users.username username]}))
