{{- define "customers-service.name" -}}
customers-service
{{- end }}

{{- define "customers-service.fullname" -}}
{{ .Release.Name }}
{{- end }}
