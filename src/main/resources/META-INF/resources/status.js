document.addEventListener('DOMContentLoaded', function() {
    addListeners();
}, false);


function addListeners(){
    document.getElementById("btnReport").addEventListener("click", function() {
        var btn = document.getElementById("btnReport");
        var t = document.getElementById("txtTokenSubscribe").value;
        var e = document.getElementById("txtEmailSubscribe").value;
        var r = document.getElementById("txtReposSubscribe").value.split("\n");
        
        var details = {
            'token': t,
            'email': e,
            'repositories': r
        };
    
        post("/createReport", details, "Status report send to " + e, btn);
    });

    document.getElementById("btnSubscribe").addEventListener("click", function() {
        var btn = document.getElementById("btnSubscribe");
        var t = document.getElementById("txtTokenSubscribe").value;
        var e = document.getElementById("txtEmailSubscribe").value;
        var r = document.getElementById("txtReposSubscribe").value.split("\n");
        
        var details = {
            'token': t,
            'email': e,
            'repositories': r
        };
    
        post("/subscribe", details, e + " subscribed to weekly status", btn);
    });

    document.getElementById("btnUnsubscribe").addEventListener("click", function() {
        var btn = document.getElementById("btnUnsubscribe");
        var e = document.getElementById("txtEmailUnsubscribe").value;
        
        var details = {
            'email': e
        };
        
        post("/unsubscribe", details, e + " unsubscribed from weekly status", btn);
    });
    
    document.getElementById("btnHistory").addEventListener("click", function() {
        var btn = document.getElementById("btnHistory");
        var e = document.getElementById("txtEmailHistory").value;
        
        var details = {
            'email': e
        };
        
        startWait(btn);
        
        var formBody = [];
        for (var property in details) {
            var encodedKey = encodeURIComponent(property);
            var encodedValue = encodeURIComponent(details[property]);
            formBody.push(encodedKey + "=" + encodedValue);
        }
        formBody = formBody.join("&");

        fetch("/history", {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'
            },
            body: formBody
        }).then(response => response.json())
          .then(data => {
            var accordion = document.getElementById("accordionHistory");
            
            data.forEach(function(report, index) {
                
                accordion.innerHTML = "";
                
                console.log(report); 
                var accordionItemDiv = document.createElement("div");
                accordionItemDiv.classList.add("accordion-item");
                // Header
                var accordionItemHeader = document.createElement("h2");
                accordionItemHeader.classList.add("accordion-header");
                accordionItemHeader.setAttribute("id","accordionItemHeader" + index);
                
                
                var accordionItemHeaderButton = document.createElement("button");
                accordionItemHeaderButton.classList.add("accordion-button");
                accordionItemHeaderButton.classList.add("collapsed");
                accordionItemHeaderButton.setAttribute("type","button");
                accordionItemHeaderButton.setAttribute("data-bs-toggle","collapse");
                accordionItemHeaderButton.setAttribute("data-bs-target","#accordionItemCollapse" + index);
                accordionItemHeaderButton.setAttribute("aria-expanded","false");
                accordionItemHeaderButton.setAttribute("aria-expanded","accordionItemCollapse" + index);
                
                accordionItemHeaderButton.innerText = report.date;
                
                accordionItemHeader.appendChild(accordionItemHeaderButton);
                
                accordionItemDiv.appendChild(accordionItemHeader);
                
                // Collapse
                var accordionItemCollapse = document.createElement("div");
                accordionItemCollapse.classList.add("accordion-collapse");
                accordionItemCollapse.classList.add("collapse");
                accordionItemCollapse.setAttribute("id","accordionItemCollapse" + index);
                accordionItemCollapse.setAttribute("aria-labelledby","accordionItemHeader" + index);
                accordionItemCollapse.setAttribute("data-bs-parent","#accordionHistory");
                
                var accordionItemCollapseBody = document.createElement("div");
                accordionItemCollapseBody.classList.add("accordion-body");
                
                var doneHeader = document.createElement("h5")
                doneHeader.innerHTML = "<div class='shadow-sm p-2 mb-3 bg-body rounded'><i class='bi bi-list-check'></i> Done </div>";
                accordionItemCollapseBody.appendChild(doneHeader);
                
                var repos = report.repos;
                repos.forEach(function(repo) {
                    var dones = repo.done;
                    if(dones.length > 0){
                        var repoHeader = document.createElement("b");
                        repoHeader.innerText = repo.name;
                        accordionItemCollapseBody.appendChild(repoHeader);
                    
                        var doneList = document.createElement("ul");
                        dones.forEach(function(done) {
                        
                            var doneItem = document.createElement("li");
                            doneItem.innerHTML = "<a href='" + done.url + "' target='_blank' class='text-decoration-none'>" + done.title + " </a>";
                            doneList.appendChild(doneItem);
                        });
                    
                        accordionItemCollapseBody.appendChild(doneList);
                    }  
                });
                
                var todoHeader = document.createElement("h5");
                todoHeader.innerHTML = "<div class='shadow-sm p-2 mb-3 bg-body rounded'><i class='bi bi-list-task'></i> Todo </div>";
                accordionItemCollapseBody.appendChild(todoHeader);
                
                repos.forEach(function(repo) {
                    var todos = repo.todo;
                    if(todos.length > 0){
                        var repoHeader = document.createElement("b");
                        repoHeader.innerText = repo.name;
                        accordionItemCollapseBody.appendChild(repoHeader);
                    
                        var todoList = document.createElement("ul");
                        todos.forEach(function(todo) {
                        
                            var todoItem = document.createElement("li");
                            todoItem.innerHTML = "<a href='" + todo.url + "' target='_blank' class='text-decoration-none'>" + todo.title + " </a>";
                            todoList.appendChild(todoItem);
                        });
                    
                        accordionItemCollapseBody.appendChild(todoList);
                    }  
                });
                
                accordionItemCollapse.appendChild(accordionItemCollapseBody);
                accordionItemDiv.appendChild(accordionItemCollapse);
                accordion.appendChild(accordionItemDiv);
            
            });
            
            stopWait(btn);
        }).catch((error) => {
            showErrorMessage(error);
            stopWait(btn);
        });
    });
}

function post(path, details, message, btn){
    startWait(btn);
    
    var formBody = [];
    for (var property in details) {
        var encodedKey = encodeURIComponent(property);
        var encodedValue = encodeURIComponent(details[property]);
        formBody.push(encodedKey + "=" + encodedValue);
    }
    formBody = formBody.join("&");

    fetch(path, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'
        },
        body: formBody
    }).then(data => {
        if(data.status === 202){
            showSuccessMessage(message);
        }else{
            const reason = data.headers.get('reason');
            showErrorMessage(data.status + ": " + reason);
        }    
        stopWait(btn);
    }).catch((error) => {
        showErrorMessage(error);
        stopWait(btn);
    });
}

function startWait(btn){
    var body = document.body;
    btn.disabled = true;
    body.classList.add("wait");
}

function stopWait(btn){
    var body = document.body;
    btn.disabled = false;
    body.classList.remove("wait");
}

function showErrorMessage(error){
    var element = document.getElementById("alert");
    element.classList.add("show");
    element.classList.add("alert-danger");
    element.classList.remove("alert-success");
    element.innerHTML = error + '<button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>';
}

function showSuccessMessage(message){
    var element = document.getElementById("alert");
    element.classList.add("show");
    element.classList.add("alert-success");
    element.classList.remove("alert-danger");
    element.innerHTML = message + '<button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>';
}