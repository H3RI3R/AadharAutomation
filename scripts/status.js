/**
 * 
 */
window.addEventListener('load', async function() {
    const urlParams = new URLSearchParams(window.location.search);
    const orderId = urlParams.get('order_id');
    const response = await fetch(`http://103.101.59.60/API/checkStatus?order_id=${orderId}`, {
        method: 'POST'
    });
    const data = await response.json();
    if (data.status === 'success') {
        const statusInfo = document.getElementById('statusInfo');
        statusInfo.innerHTML = `
            <p>Name: ${data.data.Name}</p>
            <p>Aadhar Number: ${data.data.AadharNumber}</p>
            <p>Gender: ${data.data.Gender}</p>
            <p>Date of Birth: ${data.data.DateOfBirth}</p>
            <p>Address: ${data.data.AadharAddress}</p>
            <p>Age: ${data.data.Age}</p>
        `;
    } else {
        alert('Error retrieving status.');
    }
});
