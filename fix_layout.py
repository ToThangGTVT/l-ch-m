import re

with open('app/src/main/res/layout/widget_month.xml', 'r') as f:
    content = f.read()

def replacer(match):
    id_str = match.group(1)
    return f'<FrameLayout android:layout_width="0dp" android:layout_height="match_parent" android:layout_weight="1"><TextView android:id="@+id/{id_str}" android:layout_width="26dp" android:layout_height="26dp" android:layout_gravity="center" android:gravity="center" android:textColor="#1D1B1E" android:textSize="10sp" android:includeFontPadding="false" /></FrameLayout>'

regex = r'<TextView[^>]*id="@\+id/(cell_\d_\d)"[^>]*>'

new_content = re.sub(regex, replacer, content)

with open('app/src/main/res/layout/widget_month.xml', 'w') as f:
    f.write(new_content)

print(f"Replaced {len(re.findall(regex, content))} occurrences")
