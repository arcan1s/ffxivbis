function loadHeader(partyId) {
    const title = $("#navbar-title");

    // because I don't know how to handle relative url if current does not end with slash
    title.attr("href", `/party/${partyId}`);
    $("#navbar-bis").attr("href", `/party/${partyId}/bis`);
    $("#navbar-loot").attr("href", `/party/${partyId}/loot`);
    $("#navbar-users").attr("href", `/party/${partyId}/users`);

    $.ajax({
        url: `/api/v1/party/${partyId}/description`,
        type: "GET",
        dataType: "json",
        success: function (resp) {
            title.text(safe(resp.partyAlias || partyId));
        },
        error: function (jqXHR, _, errorThrown) { requestAlert(jqXHR, errorThrown); },
    });
}

function loadTypes(url, selector) {
    $.ajax({
        url: url,
        type: "GET",
        dataType: "json",
        success: function (data) {
            const options = data.map(function (name) {
                const option = document.createElement("option");
                option.value = name;
                option.innerText = name;
                return option;
            });
            selector.empty().append(options);
        },
        error: function (jqXHR, _, errorThrown) { requestAlert(jqXHR, errorThrown); },
    });
}

function setupFormClear(dialog, reset) {
    dialog.on("hide.bs.modal", function () {
        $(this).find("form").trigger("reset");
        $(this).find("table").bootstrapTable("removeAll");
        if (reset) {
            reset();
        }
    });
}

function setupRemoveButton(table, removeButton) {
    table.on("check.bs.table uncheck.bs.table check-all.bs.table uncheck-all.bs.table",
        function () {
            removeButton.prop("disabled", !table.bootstrapTable("getSelections").length);
        });
}
