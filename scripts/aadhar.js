document.getElementById('aadharForm').addEventListener('submit', async function(event) {
    event.preventDefault(); // Prevent the default form submission

    const aadharNumber = document.getElementById('aadhar').value;

    try {
        const response = await fetch(`http://localhost:8080/AadharAutomate/add?ad=${aadharNumber}`, {
            method: 'POST'
        });

        if (!response.ok) {
            throw new Error('Network response was not ok');
        }

        const data = await response.json();

        if (data.order_id) {
            window.location.href = `otp.html?order_id=${data.order_id}`;
        } else {
            alert('Error: Order ID not received.');
        }
    } catch (error) {
        console.error('Error:', error);
        alert('An error occurred. Please try again.');
    }
});
