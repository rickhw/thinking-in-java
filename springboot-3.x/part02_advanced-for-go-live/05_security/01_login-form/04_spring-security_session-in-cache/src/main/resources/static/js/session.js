document.addEventListener("DOMContentLoaded", function() {
    document.getElementById('fetchSessionInfo').addEventListener('click', function() {
        fetchSessionInfo();
    });
});

function fetchSessionInfo() {
    fetch('/session-info')
        .then(response => response.json())
        .then(data => {
            document.getElementById('sessionId').textContent = data.sessionId;
            document.getElementById('creationTime').textContent = data.creationTime;
            document.getElementById('lastAccessedTime').textContent = data.lastAccessedTime;
            document.getElementById('maxInactiveInterval').textContent = data.maxInactiveInterval;
            document.getElementById('isNew').textContent = data.isNew;

            const attributesBody = document.getElementById('attributesBody');
            attributesBody.innerHTML = ''; // Clear existing rows
            for (const [key, value] of Object.entries(data.attributes)) {
                if (key === 'SPRING_SECURITY_CONTEXT') {
                    for (const [innerKey, innerValue] of Object.entries(value)) {
                        if (innerKey === 'authentication') {
                            for (const [authKey, authValue] of Object.entries(innerValue)) {
                                const row = document.createElement('tr');
                                const nameCell = document.createElement('td');
                                const valueCell = document.createElement('td');
                                nameCell.textContent = `${authKey}`;
                                valueCell.textContent = JSON.stringify(authValue);
                                row.appendChild(nameCell);
                                row.appendChild(valueCell);
                                attributesBody.appendChild(row);
                            }
                        } else {
                            const row = document.createElement('tr');
                            const nameCell = document.createElement('td');
                            const valueCell = document.createElement('td');
                            nameCell.textContent = `${key}.${innerKey}`;
                            valueCell.textContent = JSON.stringify(innerValue);
                            row.appendChild(nameCell);
                            row.appendChild(valueCell);
                            attributesBody.appendChild(row);
                        }
                    }
                } else {
                    const row = document.createElement('tr');
                    const nameCell = document.createElement('td');
                    const valueCell = document.createElement('td');
                    nameCell.textContent = key;
                    valueCell.textContent = value;
                    row.appendChild(nameCell);
                    row.appendChild(valueCell);
                    attributesBody.appendChild(row);
                }
            }
        })
        .catch(error => console.error('Error fetching session info:', error));
}
