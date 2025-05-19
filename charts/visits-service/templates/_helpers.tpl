{{- define "visits-service.name" -}}
visits-service
{{- end }}

{{- define "visits-service.fullname" -}}
{{ .Release.Name }}
{{- end }}
