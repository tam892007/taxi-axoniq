let actionInProgress = false
let userId = "pax-1"
let pickUpInput
let dropOffInput
let bookButton
let notification
let notificationText
let notificationButton
let tableSource

function setDomElements() {
    pickUpInput = document.getElementById("pick-up-input")
    dropOffInput = document.getElementById("drop-off-input")
    bookButton = document.getElementById("book-button")
    notification = document.getElementById("notification")
    notificationText = document.getElementById("notification-text")
    notificationButton = document.getElementById("notification-button")
}

function setListeners() {
    pickUpInput.addEventListener("keyup", () => {
        maybeSwitchBookingState()
    })
    dropOffInput.addEventListener("keyup", () => {
        maybeSwitchBookingState()
    })
	bookButton.addEventListener("click", () => {
        void book();
    });
}

function hideNotification() {
    notification.style.visibility = "hidden"
}

function removeColorClassesFromNotification() {
    notification.classList.remove("is-success", "is-info", "is-danger")
}

function disableAllButtons() {
    bookButton.disabled = true
}

function maybeSwitchAll(){
	maybeSwitchBookingState()
}

function maybeSwitchBookingState(){
	if (bookButton.disabled && !actionInProgress && pickUpInput.value !== "" && dropOffInput.value !== "") {
        bookButton.disabled = false
    } else if (pickUpInput.value === "" || dropOffInput.value === "") {
        bookButton.disabled = true
    }
}

function createCancelButton(bookId) {
	const btn = document.createElement('input');
	btn.type = "button";
	btn.className = "button";
	btn.value = "Cancel";
	btn.addEventListener("click", () => {
        void cancel(bookId);
    });
    return btn;
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
    maybeSwitchAll()
}

async function book() {
    actionInProgress = true
    disableAllButtons()
    const pickUp = pickUpInput.value
    const dropOff = dropOffInput.value
    pickUpInput.value = ""
    dropOffInput.value = ""
    const response = await fetch("/booking/book/user/" + userId + "/pick-up/" + pickUp + "/drop-off/" + dropOff, {
        method: "POST"
    });
    response.json().then(result => handleResult(result))
}

async function cancel(bookId) {
    actionInProgress = true
    const response = await fetch("/booking/cancel/user/" + userId + "/booking/" + bookId, {
        method: "POST"
    });
    response.json().then(result => handleResult(result))
}

async function updateTable() {
    if (tableSource !== undefined){
        tableSource.close()
    }
    let tableIds = []
    const tableBody = document.getElementById("table-body")
    tableBody.innerHTML = ""
    tableSource = new EventSource("/booking/subscribe")
    tableSource.onmessage = function (event) {
        const bookingData = JSON.parse(event.data)
        const bookingId = "card_" + bookingData["bookingId"]
        if (tableIds.includes(bookingId)) {
            document.getElementById(bookingId).remove()
            tableIds = tableIds.filter(item => item !== bookingId)
        } 
        tableIds.unshift(bookingId)
        const row = tableBody.insertRow(0)
        row.id = bookingId
        const cell1 = row.insertCell(0)
        const cell2 = row.insertCell(1)
        const cell3 = row.insertCell(2)
        const cell4 = row.insertCell(3)
        const cell5 = row.insertCell(4)
        const cell6 = row.insertCell(5)
        cell1.innerHTML = bookingData["bookingId"]
        cell2.innerHTML = bookingData["pickUp"]
        cell3.innerHTML = bookingData["dropOff"]
        cell4.innerHTML = bookingData["status"]
        cell5.innerHTML = bookingData["driverId"]
        if (bookingData["status"] == "ACTIVE") {
        	cell6.appendChild(createCancelButton(bookingData["bookingId"]))
        }
    };
}

window.addEventListener("load", () => {
    setDomElements()
    setListeners()
    void updateTable()
})