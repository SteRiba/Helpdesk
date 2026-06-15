import {getToken, logout} from "./auth-utils.js";
import {showToast} from "./toast.js";

const logoutBtn = document.querySelector(".logout-btn");
const ticketForm = document.querySelector(".ticket-form");
const ticketListEl = document.querySelector(".ticket-list");
const summaryEl = document.querySelector(".summary-card");
const submitTicketBtn = document.querySelector("#submit-ticket-btn");

function renderTickets(tickets) {
    if (tickets.length === 0) {
        ticketListEl.innerHTML = `
            <article class="ticket-empty">
                <strong>Nessun ticket trovato.</strong>
                <span>Quando apri una richiesta, comparirà qui.</span>
            </article>
        `;
        return;
    }

    summaryEl.innerHTML = `
        <span class="summary-value">Tickets aperti: ${tickets.length}</span>
        <span>Richieste personali</span>
    `;

    ticketListEl.innerHTML = tickets.map((ticket) => `
        <article class="ticket-item">
            <div class="ticket-item-header">
                <div>
                    <h3>${ticket.title}</h3>
                    <p>${ticket.description}</p>
                </div>
                <span class="pill pill-status">${ticket.status}</span>
            </div>
            <div class="ticket-meta">
                <span class="pill pill-priority">${ticket.priority}</span>
                <span class="pill">${ticket.category}</span>
            </div>
        </article>
    `).join("");
}

async function getMyTickets() {
    const token = getToken();

    if (!token) {
        window.location.replace("/pages/login.html");
        return;
    }

    try {
        const response = await fetch("/api/v1/tickets", {
            method: "GET",
            headers: {
                "Authorization": `Bearer ${token}`
            }
        });

        if (!response.ok) {
            throw new Error("Non sono riuscito a recuperare le tue richieste.");
        }

        const tickets = await response.json();
        renderTickets(tickets);
    } catch (error) {
        showToast("error", "Ticket non caricati", error.message);
    }
}

async function createTicket(event) {
    event.preventDefault();

    const token = getToken();

    if (!token) {
        return;
    }

    const formData = new FormData(ticketForm);
    const ticket = {
        title: formData.get("title"),
        category: formData.get("category"),
        priority: formData.get("priority"),
        description: formData.get("description")
    };

    submitTicketBtn.disabled = true;

    try {
        const response = await fetch("/api/v1/tickets", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`
            },
            body: JSON.stringify(ticket)
        });

        if (!response.ok) {
            throw new Error("Controlla i dati inseriti e riprova.");
        }

        ticketForm.reset();
        await getMyTickets();
        showToast("success", "Ticket creato", "Ticket aperto correttamente.");
    } catch (error) {
        showToast("error", "Ticket non creato", error.message);
    } finally {
        submitTicketBtn.disabled = false;
    }
}

ticketForm?.addEventListener("submit", createTicket);

logoutBtn?.addEventListener("click", (event) => {
    event.preventDefault();
    logout();
});

getMyTickets();
