let actionInProgress = false
let driverId = "driver-1"
let notification
let notificationText
let notificationButton
let tableSource

function setDomElements() {
    notification = document.getElementById("notification")
    notificationText = document.getElementById("notification-text")
    notificationButton = document.getElementById("notification-button")
}

function setListeners() {
}

function hideNotification() {
    notification.style.visibility = "hidden"
}

function removeColorClassesFromNotification() {
    notification.classList.remove("is-success", "is-info", "is-danger")
}

function disableAllButtons() {
}

function createAcceptButton(jobId) {
	const btn = document.createElement('input');
	btn.type = "button";
	btn.className = "button";
	btn.value = "Accept";
	btn.addEventListener("click", () => {
        void accept(jobId);
    });
    return btn;
}

async function accept(jobId) {
    actionInProgress = true
    const response = await fetch("/job/accept/driver/" + driverId + "/job/" + jobId, {
        method: "POST"
    });
    response.json().then(result => handleResult(result))
}

async function handleResult(result) {
    actionInProgress = false
    hideNotification()
    removeColorClassesFromNotification()
    if (result["isSuccess"] === true) {
        notification.classList.add("is-success")
        notificationText.innerHTML = "Success"
    } else {
        notification.classList.add("is-danger")
        notificationText.innerHTML = result["error"]
    }
    notification.style.visibility = ""
}


async function updateTable() {
	if (tableSource !== undefined){
        tableSource.close()
    }
    let tableIds = []
    const tableBody = document.getElementById("table-body")
    tableBody.innerHTML = ""
    tableSource = new EventSource("/job/subscribe")
    tableSource.onmessage = function (event) {
        const jobData = JSON.parse(event.data)
        const jobId = "card_" + jobData["jobId"]
        if (tableIds.includes(jobId)) {
            document.getElementById(jobId).remove()
            tableIds = tableIds.filter(item => item !== jobId)
        } 
        tableIds.unshift(jobId)
        const row = tableBody.insertRow(0)
        row.id = jobId
        const cell1 = row.insertCell(0)
        const cell2 = row.insertCell(1)
        const cell3 = row.insertCell(2)
        cell1.innerHTML = jobData["jobId"]
        cell2.innerHTML = jobData["status"]
        if (jobData["status"] == "PENDING"){
        	cell3.appendChild(createAcceptButton(jobData["jobId"]))
		}
    };
}

window.addEventListener("load", () => {
    setDomElements()
    setListeners()
    void updateTable()
})