function searchTable() {
    var input = document.getElementById("search");
    var filter = input.value.toLowerCase();
    var table = document.getElementById("result");
    var tr = table.getElementsByTagName("tr");

    // from 1 coz of header
    for (var i = 1; i < tr.length; i++) {
        var td = tr[i].getElementsByClassName("include_search");
        var display = "none";
        for (var j = 0; j < td.length; j++) {
            if (td[j].tagName.toLowerCase() === "td") {
                if (td[j].innerHTML.toLowerCase().indexOf(filter) > -1) {
                    display = "";
                    break;
                }
            }
        }
        tr[i].style.display = display;
    }
}
