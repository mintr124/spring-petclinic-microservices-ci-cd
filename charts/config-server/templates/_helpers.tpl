{{- define "config-server.name" -}}
config-server
{{- end }}

{{- define "config-server.fullname" -}}
{{ .Release.Name }}
{{- end }}
