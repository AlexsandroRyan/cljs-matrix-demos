(ns vouch.matrix.app
  (:require ["matrix-js-sdk" :as matrix.sdk]))

(def login-params
  #js {:type "m.login.password",
       :user "alex",
       :password "Alex@1234"})

(defn main [& cli-args]
  (let [client (matrix.sdk/createClient #js {:baseUrl "http://localhost:8008"})
        login-dt
        (-> (.loginRequest client login-params)
            (.then (fn [the-result]
                     (prn the-result)))
            (.catch (fn [failure]
                      (prn [:oh-oh failure]))))
        _ (println login-dt)]))

(comment
  (do (require '[shadow.cljs.devtools.server :as shadow.server]
               '[shadow.cljs.devtools.api :as shadow])
      (shadow.server/start!)
      (shadow/watch :script)
      (shadow/nrepl-select :script)))

