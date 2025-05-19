{{- define "genai-service.name" -}}
genai-service
{{- end }}

{{- define "genai-service.fullname" -}}
{{ .Release.Name }}
{{- end }}
