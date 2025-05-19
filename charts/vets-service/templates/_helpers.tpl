{{- define "vets-service.name" -}}
vets-service
{{- end }}

{{- define "vets-service.fullname" -}}
{{ .Release.Name }}
{{- end }}
