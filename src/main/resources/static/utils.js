function createAlert(message, placeholder) {
    const wrapper = document.createElement('div');
    wrapper.innerHTML = `<div class="alert alert-danger alert-dismissible" role="alert">${safe(message)}<button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button></div>`;
    placeholder.append(wrapper);
}

function formatPlayerId(obj) {
    return `${obj.nick} (${obj.job})`;
}

function getCurrentOption(select) {
    return select.find(":selected")[0];
}

function getPartyId() {
    const request = new XMLHttpRequest();
    request.open("HEAD", document.location, false);
    request.send(null);

    // tuple lol
    return [
        request.getResponseHeader("X-Party-Id"),
        request.getResponseHeader("X-User-Permission") === "get",
    ]
}

function requestAlert(jqXHR, errorThrown) {
    let message;
    try {
        message = $.parseJSON(jqXHR.responseText).message;
    } catch (_) {
        message = errorThrown;
    }
    const alert = $("#alert-placeholder");
    createAlert(`Error during request: ${message}`, alert);
}

function safe(string) {
    return String(string)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;");
}
