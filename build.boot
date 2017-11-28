(def project 'kongauth)
(def version "0.1.0-SNAPSHOT")

(set-env! :resource-paths #{"resources" "src"}
          :source-paths   #{"test"}
          :dependencies   '[[org.clojure/clojure "1.9.0-RC1"]
                            [org.danielsz/system "0.4.1"]
                            [clj-time "0.14.0"]
                            [cljs-ajax "0.7.2"]
                            [environ "1.1.0"]
                            [boot-environ "1.1.0"]
                            [crypto-password "0.2.0"]
                            [ring/ring-jetty-adapter "1.6.2"]
                            [org.clojure/java.jdbc "0.7.3"]
                            [org.clojure/tools.cli "0.3.5"]
                            [orchestra "2017.08.13"]
                            [expound "0.3.0"]
                            [conman "0.7.4"]
                            [honeysql "0.9.1"]
                            [phrase "0.1-alpha1"]
                            [org.clojure/tools.logging "0.4.0"]
                            [clj-http "3.7.0"]
                            [buddy/buddy-auth "2.1.0"]
                            [metosin/ring-http-response "0.9.0"]
                            [compojure "1.6.0"]
                            [org.postgresql/postgresql "9.4-1201-jdbc41"]
                            [ring "1.6.3"]
                            [slingshot "0.12.2"]
                            [ring/ring-defaults "0.3.1"]
                            [ring-middleware-format "0.7.2"]
                            [com.cognitect/transcriptor "0.1.5"]
                            [ragtime "0.7.2"]
                            [cheshire "5.8.0"]
                            [nightlight "1.9.3" :scope "test"]
                            [adzerk/boot-reload "0.5.2" :scope "test"]
                            [adzerk/boot-test "RELEASE" :scope "test"]])

(require '[system.boot :refer [system run]]
         '[kongauth.systems :refer [dev-system]]
         '[adzerk.boot-reload :refer :all]
         '[clojure.edn :as edn]
         '[environ.core :refer [env]]
         '[environ.boot :refer [environ]])


(require '[nightlight.boot :refer [nightlight]])
(require '[ragtime.jdbc :as jdbc]
         '[ragtime.repl :as repl])
(require '[kongauth.db.util :as dbutil])

(task-options!
 aot {:namespace   #{'kongauth.core}}
 pom {:project     project
      :version     version
      :description "FIXME: write description"
      :url         "http://example/FIXME"
      :scm         {:url "https://github.com/yourname/kongauth"}
      :license     {"Eclipse Public License"
                    "http://www.eclipse.org/legal/epl-v10.html"}}
 jar {:main        'kongauth.core
      :file        (str "kongauth-" version "-standalone.jar")})

(def read-edn (comp edn/read-string slurp))

(def profile (fn []
               (read-edn "profiles.edn")))

(defn config []
  {:datastore  (jdbc/sql-database (dbutil/get-db-spec-from-env :config
                                                               (profile)))
   :migrations (jdbc/load-resources "migrations")})

(deftask migrate
  "Task to run a db migration"
  []
  (with-pre-wrap [fs]
    (repl/migrate (config))
    fs))

(deftask rollback
  "Task to run a db rollback"
  []
  (with-pre-wrap [fs]
    (repl/rollback (config))
    fs))

(deftask local-migrate []
  (comp  (environ :env (profile))
         (migrate)))

(deftask local-rollback []
  (comp (environ :env (profile))
        (rollback)))

(deftask dev
  "run a restartable system"
  []
  (comp
   (environ :env (profile))
   (watch :verbose true)
   (system :sys #'dev-system
           :auto true
           :files ["routes.clj" "systems.clj"])
   (nightlight :port 4000)
   (repl :server true
         :host "127.0.0.1"
         :port 8989)))

(deftask build
  "Build the project locally as a JAR."
  [d dir PATH #{str} "the set of directories to write to (target)."]
  (let [dir (if (seq dir) dir #{"target"})]
    (comp (aot) (pom) (uber) (jar) (target :dir dir))))

(deftask run-project
  "Run the project."
  [a args ARG [str] "the arguments for the application."]
  (require '[kongauth.core :as app])
  (apply (resolve 'app/-main) args))

(require '[adzerk.boot-test :refer [test]])
