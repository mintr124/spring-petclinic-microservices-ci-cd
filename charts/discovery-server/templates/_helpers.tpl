{{- define "discovery-server.name" -}}
discovery-server
{{- end }}

{{- define "discovery-server.fullname" -}}
{{ .Release.Name }}
{{- end }}
