(ns redcap.unit
  (:require [std.lib :as h :refer [defimpl]]
            [std.string :as str]
            [redcap.meta :as meta]))

(defmulti unit-invoke
  "invokes a redcap api unit"
  {:added "0.1"}
  :op)

(defmethod unit-invoke :default
  [m]
  (into {} m))

(defn unit-display
  "displays a unit"
  {:added "0.1"}
  [{:keys [input defaults spec]
    :as m}]
  (let [{:keys [category tag]} input]
    [category tag]))

(defn unit-string
  "creates a unit string"
  {:added "0.1"}
  [m]
  (str "#redcap.unit" (unit-display m)))

(defimpl Unit [input defaults spec]
  :type defrecord
  :invoke [unit-invoke 1]
  :string unit-string
  :final true)

(defn generate-unit-spec
  "generates a unit spec"
  {:added "0.1"}
  [{:keys [category
           tag
           content
           action
           params]
    :as input}]
  (apply vector
         :map
         [:token    meta/TokenSpec]
         [:content  [:enum content]]
         [:action   [:enum action]]
         [:format   {:optional true}     meta/FormatSpec]
         [:returnFormat {:optional true} meta/FormatSpec]
         (map (fn [[k m]]
                (let [{:keys [transform-key
                              type
                              required]}  m
                      nk  ((str/wrap (or transform-key
                                         str/camel-case)) k)]
                  [nk (if required {} {:optional true})
                   (or type
                       (throw (ex-info "Type Missing" {:key key
                                                       :spec m})))]))
              (sort params))))

(defn generate-unit-defaults
  "generate unit defaults"
  {:added "0.1"}
  [{:keys [category
           tag
           content
           action
           params]
    :as input}]
  (->> params
       (keep (fn [[k m]]
               (let [{:keys [transform-key
                             default]}  m
                     nk  ((str/wrap (or transform-key
                                        str/camel-case)) k)]
                 (when default
                   [nk default]))))
       (into {:action action
              :content content})))

(defn generate-unit
  "creates a unit"
  {:added "0.1"}
  [{:keys [category
           tag
           content
           params]
    :as input}]
  (let [spec     (generate-unit-spec input)
        defaults (generate-unit-defaults input)]
    (map->Unit
     {:input    input
      :defaults defaults
      :spec     spec})))

