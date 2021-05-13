# IMLocation

IMLocation - Android-приложения для навигации в помещении по WI-FI. Предполагается, что для полного функционирования системы необходимо загрузить схемы здания, развернуть [готовый сервер](https://github.com/RTUITLab/WifiLocalPositioningServer.git) и обучить систему с помощью созданной для этого внутри приложения панели. Настроенная в приложении система уровней доступа может позволять вносить вклад в обучение системы любому пользователю.

# Пользователь

Для пользователя доступен следующий функционал:
- удобный просмотр всех предоставленных схем этажей здания (см Рис. 1)
- отображения текущего местоположения пользователя по результатам сканирования (см Рис. 2)
- перенос фокуса на текущее местоположение пользователя на схеме по нажатию на специальную клавишу (см Рис. 2)
- панель поиска с автоматическим дополнением, которая поможет пользователю определить местоположение введённого им участка по его названию (дополняется текущим местоположением) (см Рис. 3)
- панель построения маршрута, которая даёт пользователю возможность увидеть маршрут от точки до точки (используется то же автодополнение и отображение текущего местоположения, в путь встроены знаки лестниц, начала маршрута и его конца) (см Рис. 4, Рис. 5)
- возможность изменять частоту сканирований, количество сканирований, а также проверять текущий уровень доступа и получать идентификатор устройства - всё это находиться внутри меню настроек (см Рис. 6)
- сообщения о некоторых ограничениях частоты сканирования WI-FI в связи с определённой версией OC Adnroid и инструкции по их устранению  (см Рис. 7)

| Рис. 1 | Рис. 2 |
|----------------|:----------------:|
| ![Oops! Something went wrong](https://github.com/mrkiriss/WifiLocalPositioning/blob/main/img_demo/screen.jpg)|![Oops! Something went wrong](https://github.com/mrkiriss/WifiLocalPositioning/blob/main/img_demo/current_location.jpg) |

| Рис. 3 | Рис. 4 |
|----------------|:----------------:|
| ![Oops! Something went wrong](https://github.com/mrkiriss/WifiLocalPositioning/blob/main/img_demo/autocomplete.jpg) |![Oops! Something went wrong](https://github.com/mrkiriss/WifiLocalPositioning/blob/main/img_demo/route_with_start.jpg) |

| Рис. 5 | Рис. 6 | Рис. 7 |
|----------------|:----------------:|----------------:|
| ![Oops! Something went wrong](https://github.com/mrkiriss/WifiLocalPositioning/blob/main/img_demo/route_with_end.jpg) |![Oops! Something went wrong](https://github.com/mrkiriss/WifiLocalPositioning/blob/main/img_demo/settings.png) | ![Oops! Something went wrong](https://github.com/mrkiriss/WifiLocalPositioning/blob/main/img_demo/abilities_message.jpg) |
# Администратор

Чтобы приложение полностью раскрыло свои возможности, необходимо предварительно обучить систему. Для этого было разработано специальное меню, доступ к которому имеет только администратор - человек с определённым уровнем доступа в приложении.
Для администратора, в меню обучении системы, доступен следующий функционал:
- отдельная панель с возможностями сканирования, для удобного изучения доступных точек доступа WI-FI, с возможностями определения местоположения и тренировки сканированиями (см Рис. 8)
- удобное обновление и отображение всех точек, созданных ранее и сохранённых на сервере, на схемах этажей, внутри специального экрана
- элемент отображения ответа сервера, для оперативной проверки работоспособности системы и правильности обработанных сервером данных
- несколько режимов тренировки системы в виде:
  - меню создания точки со следующими возможностями (см Рис. 9):
    - выбор точки с помощью двойного клика по схеме этажа или путём ввода необходимых данных в соответствующие элементы экрана
    - отображение точки по введённым данным с помощью кнопки
    - отправка данных о точке на сервер
  - меню тренировки созданной точки сканированиями с возможностями (см Рис. 10):
    - выбора точки для тренировки этажа по двойному клику на неё на схеме
    - указания количества необходимых сканирований и запуск сканирований
    - отображение результатов сканирований в прокручиваемом списке
  - меню выбора соседей точки для построения маршрутов позволяет (см Рис. 11):
    - выбирать точку для редактирования по двойному нажатию на ней на схеме этажа
    - получать для выбранной точки данные о её текущих связях с сервера
    - добавлять или удалять точки из связей редактируемой точки
    - отправлять окончательный список связей на сервер или отменять действие
  - меню удаления информации о точках с возможностями (см Рис. 12):
    - выбора точки для редактирования с помощью двойного нажатия на неё на схеме этажа или с помощью ввода её названия
    - удаления информации о сканированиях для выбранной точки с сервера
    - удаления всей информации о выбранной точке с сервера

| Рис. 8 | Рис. 9 | Рис. 10 |
|----------------|:----------------:|----------------:|
| ![Oops! Something went wrong](https://github.com/mrkiriss/WifiLocalPositioning/blob/main/img_demo/scanning%2B_menu.jpg) |![Oops! Something went wrong](https://github.com/mrkiriss/WifiLocalPositioning/blob/main/img_demo/add_information_menu.jpg) | ![Oops! Something went wrong](https://github.com/mrkiriss/WifiLocalPositioning/blob/main/img_demo/scanning_menu.jpg) |

| Рис. 11 | Рис. 12 |
|----------------|:----------------:|
| ![Oops! Something went wrong](https://github.com/mrkiriss/WifiLocalPositioning/blob/main/img_demo/connections_menu.jpg) |![Oops! Something went wrong](https://github.com/mrkiriss/WifiLocalPositioning/blob/main/img_demo/deleting_menu.jpg) |
