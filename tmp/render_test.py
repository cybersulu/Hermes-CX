import subprocess
import os

svg_content = """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 108 108" width="108" height="108">
  <defs>
    <linearGradient id="bg" x1="0" y1="0" x2="108" y2="108" gradientUnits="userSpaceOnUse">
      <stop offset="0%" stop-color="#0F172A"/>
      <stop offset="100%" stop-color="#1E293B"/>
    </linearGradient>
  </defs>
  <!-- Background -->
  <rect width="108" height="108" fill="url(#bg)"/>
  <circle cx="54" cy="54" r="40" fill="none" stroke="#1E3A5F" stroke-width="1.5"/>

  <!-- Top exchange arrow curving clockwise -->
  <!-- Outer radius 33.5, Inner radius 26.5. Center (54,54). Thickness 7.
       Fills 66dp-70dp standard circle -->
  <path fill="#10B981" d="M54,20.5 C72.5,20.5 87.5,35.5 87.5,54 L93.5,54 L83.5,67 L73.5,54 L79.5,54 C79.5,40.0 68.0,28.5 54,28.5 C47.5,28.5 41.5,31.2 37.3,35.7 L30.8,29.2 C36.8,23.8 45.0,20.5 54,20.5 Z"/>

  <!-- Bottom exchange arrow curving counter-clockwise -->
  <path fill="#06B6D4" d="M54,87.5 C35.5,87.5 20.5,72.5 20.5,54 L14.5,54 L24.5,41 L34.5,54 L28.5,54 C28.5,68.0 40.0,79.5 54,79.5 C60.5,79.5 66.5,76.8 70.7,72.3 L77.2,78.8 C71.2,84.2 63.0,87.5 54,87.5 Z"/>

  <!-- Caduceus Symbol in Center -->
  <!-- 1. Central Staff -->
  <circle cx="54" cy="35" r="2.8" fill="#F8FAFC"/>
  <path d="M52.8,37.5 L55.2,37.5 L55.2,71 L54,73.5 L52.8,71 Z" fill="#F8FAFC"/>

  <!-- 2. Wings -->
  <!-- Left Wing -->
  <path d="M53,38.5 C48,36.5 43.5,35 39,36 C40.5,38 41.5,40 40.5,42 C43,43 46,43 49.5,42.5 C51.5,42 52.8,40.5 53,38.5 Z" fill="#F8FAFC"/>
  <!-- Right Wing -->
  <path d="M55,38.5 C60,36.5 64.5,35 69,36 C67.5,38 66.5,40 67.5,42 C65,43 62,43 58.5,42.5 C56.5,42 55.2,40.5 55,38.5 Z" fill="#F8FAFC"/>

  <!-- 3. Entwined Serpents -->
  <!-- Left Snake: Head at (48, 44.5), loops out to left, crosses staff at 52, loops right at 59, crosses at 65, tails at 70 -->
  <path d="M49,44.5 C46,45.5 44,48 45,51.5 C46,55 52,56.5 54,58.5 C56,60.5 61,62 61,65 C61,67.5 58,69.5 54,70.5"
        fill="none" stroke="#F8FAFC" stroke-width="2.2" stroke-linecap="round"/>
  <!-- Left Snake Head -->
  <path d="M47.5,43 L50.5,44.5 L48,46 Z" fill="#F8FAFC"/>

  <!-- Right Snake (Mirror): Head at (59, 44.5), loops out to right, crosses at 52, loops left at 59, crosses at 65, tails at 70 -->
  <path d="M59,44.5 C62,45.5 64,48 63,51.5 C62,55 56,56.5 54,58.5 C52,60.5 47,62 47,65 C47,67.5 50,69.5 54,70.5"
        fill="none" stroke="#F8FAFC" stroke-width="2.2" stroke-linecap="round"/>
  <!-- Right Snake Head -->
  <path d="M60.5,43 L57.5,44.5 L60,46 Z" fill="#F8FAFC"/>

</svg>
"""

with open("/tmp/test_icon.svg", "w") as f:
    f.write(svg_content)

res = subprocess.run(["convert", "/tmp/test_icon.svg", "/tmp/test_icon.png"], capture_output=True, text=True)
print("Convert result:", res.returncode, res.stderr)
