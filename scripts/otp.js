document.getElementById('otpForm').addEventListener('submit', async function(event) {
    event.preventDefault(); // Prevent the default form submission

    const urlParams = new URLSearchParams(window.location.search);
    const orderId = urlParams.get('order_id');
    const otp = document.getElementById('otp').value;

    try {
        const formData = new FormData();
        formData.append('otp', otp);

        const response = await fetch(`http://localhost:8080/AadharAutomate/OTPInputServlet?order_id=${orderId}`, {
            method: 'POST',
            body: formData
        });

        if (!response.ok) {
            throw new Error('Network response was not ok');
        }

        const data = await response.json();

        if (data.data === 'Successful') {
            window.location.href = `status.html?order_id=${orderId}`;
        } else {
            alert('Error: OTP verification failed.');
        }
    } catch (error) {
        console.error('Error:', error);
        alert('An error occurred. Please try again.');
    }
});
