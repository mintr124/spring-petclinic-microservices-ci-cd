{{- define "api-gateway.name" -}}
api-gateway
{{- end }}

{{- define "api-gateway.fullname" -}}
{{ .Release.Name }}-gateway
{{- end }}
