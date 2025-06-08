const tempHumidityCtx = document.getElementById('tempHumidityChart').getContext('2d');
const tempHumidityChart = new Chart(tempHumidityCtx, {
    type: 'line',
    data: {
        labels: ['6AM', '9AM', '12PM', '3PM', '6PM', '9PM'],
        datasets: [
            {
                label: 'Temperature (°C)',
                data: [22, 24, 28, 30, 27, 25],
                borderColor: 'rgba(255, 99, 132, 1)',
                backgroundColor: 'rgba(255, 99, 132, 0.2)',
                fill: true,
                tension: 0.4
            },
            {
                label: 'Humidity (%)',
                data: [80, 75, 65, 60, 70, 78],
                borderColor: 'rgba(54, 162, 235, 1)',
                backgroundColor: 'rgba(54, 162, 235, 0.2)',
                fill: true,
                tension: 0.4
            }
        ]
    },
    options: {
        responsive: true,
        plugins: {
            legend: {
                position: 'top',
            },
            title: {
                display: true,
                text: 'Temperature & Humidity Throughout the Day'
            }
        }
    }
});

const soilMoistureCtx = document.getElementById('soilMoistureChart').getContext('2d');
const soilMoistureChart = new Chart(soilMoistureCtx, {
    type: 'bar',
    data: {
        labels: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'],
        datasets: [{
            label: 'Soil Moisture (%)',
            data: [35, 40, 42, 38, 45, 50, 48],
            backgroundColor: 'rgba(153, 102, 255, 0.6)',
            borderColor: 'rgba(153, 102, 255, 1)',
            borderWidth: 1
        }]
    },
    options: {
        responsive: true,
        scales: {
            y: {
                beginAtZero: true,
                max: 100
            }
        },
        plugins: {
            title: {
                display: true,
                text: 'Soil Moisture Trend Over a Week'
            }
        }
    }
});
  
    function toggleProperties() {
      const section = document.getElementById("propertySection");
      section.classList.toggle("hidden");
    }
 