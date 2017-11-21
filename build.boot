(def project 'kongauth)
(def version "0.1.0-SNAPSHOT")

(set-env! :resource-paths #{"resources" "src"}
          :source-paths   #{"test"}
          :dependencies   '[[org.clojure/clojure "RELEASE"]
                            [org.danielsz/system "0.4.1"]
                            [clj-time "0.14.0"]
                            [cljs-ajax "0.7.2"]
                            [environ "1.1.0"]
                            [boot-environ "1.1.0"]
                            [ring/ring-jetty-adapter "1.6.2"]
                            [org.clojure/java.jdbc "0.7.1"]
                            [org.clojure/tools.cli "0.3.5"]
                            [orchestra "2017.08.13"]
                            [expound "0.3.0"]
                            [phrase "0.1-alpha1"]
                            [org.clojure/tools.logging "0.4.0"]
                            [compojure "1.6.0"]
                            [org.postgresql/postgresql "9.4-1201-jdbc41"]
                            [ring "1.6.3"]
                            [adzerk/boot-reload "0.5.2" :scope "test"]
                            [adzerk/boot-test "RELEASE" :scope "test"]])

(require '[system.boot :refer [system run]]
         '[kongauth.systems :refer [dev-system]]
         '[adzerk.boot-reload :refer :all]
         '[environ.boot :refer [environ]])

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

(deftask dev
  "run a restartable system"
  []
  (comp
   (environ :env {:http-port "8080"})
   (watch :verbose true)
   (system :sys #'dev-system
           :auto true)
   (repl :server true)))

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
