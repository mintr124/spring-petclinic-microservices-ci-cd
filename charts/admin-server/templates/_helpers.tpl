{{- define "admin-server.name" -}}
admin-server
{{- end }}

{{- define "admin-server.fullname" -}}
{{ .Release.Name }}
{{- end }}
