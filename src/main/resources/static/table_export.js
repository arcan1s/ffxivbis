function downloadCsv(csv, filename) {
    var csvFile = new Blob([csv], {"type": "text/csv"});

    var downloadLink = document.createElement("a");
    downloadLink.download = filename;
    downloadLink.href = window.URL.createObjectURL(csvFile);
    downloadLink.style.display = "none";

    document.body.appendChild(downloadLink);
    downloadLink.click();
}

function exportTableToCsv(filename) {
    var table = document.getElementById("result");
    var rows = table.getElementsByTagName("tr");

    var csv = [];
    for (var i = 0; i < rows.length; i++) {
        if (rows[i].style.display === "none")
            continue;
        var cols = rows[i].querySelectorAll("td, th");

        var row = [];
        for (var j = 0; j < cols.length; j++)
            row.push(cols[j].innerText);

        csv.push(row.join(","));
    }

    downloadCsv(csv.join("\n"), filename);
}
