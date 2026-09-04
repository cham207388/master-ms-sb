{{- /*
Context dict keys:
  root  - root chart context (.)
  name  - service key / app label (e.g. accounts)
  svc   - service values map
*/ -}}
{{- define "securedbank-lib.fullname" -}}
{{- printf "%s/%s:%s" .root.Values.global.imageRegistry .svc.image .root.Values.global.imageTag -}}
{{- end -}}

{{- define "securedbank-lib.labels" -}}
app: {{ .name }}
{{- end -}}
