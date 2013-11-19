(ns clojure-screenscraping.4clojure
  (:require [net.cgrand.enlive-html :as html]
            [clj-http.client :as client]))

(def ^:dynamic *base-url* 
  "http://www.4clojure.com")
(def ^:dynamic *login-url*
  (str *base-url* "/login"))
(def ^:dynamic *problems-list-url*
  (str *base-url* "/problems"))
(def ^:dynamic *problem-base-url* 
  (str *base-url* "/problem/"))
(def ^:dynamic *solution-base-url* 
  (str *problem-base-url* "solutions/"))


(defn parse-resp-body [resp]
  (-> resp 
      :body 
      java.io.StringReader. 
      html/html-resource))

(defn login [user pass]
  (let [result (client/post *login-url* 
                            {:form-params {:user user
                                           :pass pass}})]
    (when (= 302 (:status result))
      (:cookies result))))

(defn get-logged-in-page [url cookies]
  (let [resp (client/get url 
                         {:cookies cookies})]
    (when (= 200 (:status resp))
     (parse-resp-body resp)))) 

(defn crazy-aggressive-approach [[user pass] limit]
  (let [cookies (login user pass)]
    (->> (map #(vector %
                       (get-logged-in-page (str *problem-base-url* %)
                                           cookies))
              (range 1 (inc limit)))
         (map #(vector (first %)
                       (html/select (second %) [:textarea])))
         (map (fn [[id info]] 
                (vector id (map #(first (:content %)) info)))))))
