add_executable(qrstat
	qrstat.c
	qrstat_filter.c
	qrstat_report_handler.c
	qrstat_report_handler_xml.c
	qrstat_report_handler_stdout.c
)
target_include_directories(qrstat PRIVATE
	../../common
	../../libs
	../../libs/cull
	../../libs/sgeobj
	../../libs/uti
	../common
)
target_link_libraries(qrstat PRIVATE
	usage
	sge_options
	parse_job_cull
	parse_qsub
	read_defaults
	sig_handlers
	gdi
	sgeobj
	sgeobjd
	cull
	comm
	commlists
	uti
)
