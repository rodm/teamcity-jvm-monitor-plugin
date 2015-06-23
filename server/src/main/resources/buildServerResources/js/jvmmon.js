
BS.JvmMon = {

    setBuildId: function (buildId) {
        this.buildId = buildId;
    },

    showJvmLog: function (jvmLog) {
        var url = base_uri + "/jvmmon.html"

        $j("#loadingLog").show();
        BS.ajaxRequest(url, {
            method: "GET",
            parameters: {
                buildId: this.buildId,
                logId: jvmLog
            },
            onComplete: function (xhr) {
                $j("#loadingLog").hide();

                // Insert the JVM log
                $j("#jvmLogDiv").show();
                $j("#jvmLogContainer").html(xhr.responseText);
            }
        });
    }
};
